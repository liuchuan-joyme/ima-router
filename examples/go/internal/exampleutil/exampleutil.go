package exampleutil

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"sync"
	"time"

	imarouter "github.com/liuchuan-joyme/ima-router/sdk/go"
)

const (
	defaultTestImageURL = "https://file.fashionlabs.cn/doc_image/r2v_tea_pic1.jpg"
	defaultHeadImageURL = "https://dev-jiman.oss-cn-hangzhou.aliyuncs.com/jm/20260316/ac5fe9ec6640426bafd6200b254b4b5f.png"
	defaultTailImageURL = "https://dev-jiman.oss-cn-hangzhou.aliyuncs.com/jm/20260316/321175f98af24025b835debeb18002cc.png"
)

var loadEnvOnce sync.Once

func MakeClient() (*imarouter.Client, error) {
	loadEnvOnce.Do(loadDotEnv)

	return imarouter.NewClient(
		os.Getenv("IMA_API_KEY"),
		imarouter.WithBaseURL(strings.TrimSpace(os.Getenv("IMA_BASE_URL"))),
		imarouter.WithTimeout(HTTPTimeout()),
	)
}

func HTTPTimeout() time.Duration {
	return durationFromEnv("IMA_HTTP_TIMEOUT_SECONDS", 300)
}

func ImageWaitTimeout() time.Duration {
	return durationFromEnv("IMA_IMAGE_WAIT_TIMEOUT_SECONDS", 1800)
}

func ImagePollInterval() time.Duration {
	return durationFromEnv("IMA_IMAGE_POLL_INTERVAL_SECONDS", 8)
}

func VideoWaitTimeout() time.Duration {
	return durationFromEnv("IMA_VIDEO_WAIT_TIMEOUT_SECONDS", 3600)
}

func VideoPollInterval() time.Duration {
	return durationFromEnv("IMA_VIDEO_POLL_INTERVAL_SECONDS", 10)
}

func TestImageURL() string {
	return envOr("IMA_TEST_IMAGE_URL", defaultTestImageURL)
}

func TestImageHeadURL() string {
	return envOr("IMA_TEST_IMAGE_HEAD_URL", TestImageURL())
}

func TestImageTailURL() string {
	return envOr("IMA_TEST_IMAGE_TAIL_URL", defaultTailImageURL)
}

func EnvOr(key string, fallback string) string {
	return envOr(key, fallback)
}

func EnvInt(key string, fallback int) int {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}

	parsed, err := strconv.Atoi(value)
	if err != nil {
		return fallback
	}
	return parsed
}

func EnvBool(key string, fallback bool) bool {
	value := strings.TrimSpace(os.Getenv(key))
	if value == "" {
		return fallback
	}
	return strings.EqualFold(value, "true")
}

func PrintJSON(title string, value any) {
	data, err := json.MarshalIndent(value, "", "  ")
	if err != nil {
		fmt.Printf("%s\n<json error: %v>\n", title, err)
		return
	}
	fmt.Println(title)
	fmt.Println(string(data))
}

func PrintImageOutcome(resp *imarouter.ImageTaskResponse) {
	if resp == nil {
		fmt.Println("status=<nil>")
		return
	}
	fmt.Printf("status=%s\n", resp.Data.Status)
	if resp.Data.URL != "" {
		fmt.Printf("url=%s\n", resp.Data.URL)
	}
	if resp.Data.Error != nil {
		PrintJSON("error", resp.Data.Error)
	}
}

func ImageResultURL(resp *imarouter.ImageTaskResponse) string {
	if resp == nil {
		return ""
	}
	return resp.Data.PrimaryURL()
}

func PrintVideoOutcome(resp *imarouter.VideoTaskEnvelope) {
	if resp == nil {
		fmt.Println("status=<nil>")
		return
	}
	task := resp.View()
	fmt.Printf("status=%s\n", task.Status)
	if url := task.PrimaryURL(); url != "" {
		fmt.Printf("url=%s\n", url)
	}
	if task.Error != nil {
		PrintJSON("error", task.Error)
	}
}

func PrintVideoPollStatus(resp *imarouter.VideoTaskEnvelope, attempt int) {
	if resp == nil {
		fmt.Printf("[poll %d] status=<nil> progress=<nil>\n", attempt)
		return
	}
	task := resp.View()
	fmt.Printf("[poll %d] status=%s progress=%d\n", attempt, task.Status, task.Progress)
}

func PrintMidjourneyOutcome(resp *imarouter.MidjourneyTaskResponse) {
	if resp == nil {
		fmt.Println("status=<nil>")
		return
	}
	fmt.Printf("status=%s\n", resp.Status)
	if resp.ImageURL != "" {
		fmt.Printf("imageUrl=%s\n", resp.ImageURL)
	}
	for index, url := range resp.URLs {
		fmt.Printf("urls[%d]=%s\n", index+1, url)
	}
	if resp.FailReason != "" {
		fmt.Printf("failReason=%s\n", resp.FailReason)
	}
}

func MidjourneyResultURL(resp *imarouter.MidjourneyTaskResponse) string {
	if resp == nil {
		return ""
	}
	return resp.PrimaryURL()
}

func durationFromEnv(key string, fallbackSeconds int) time.Duration {
	raw := strings.TrimSpace(os.Getenv(key))
	if raw == "" {
		return time.Duration(fallbackSeconds) * time.Second
	}

	value, err := time.ParseDuration(raw)
	if err == nil {
		return value
	}

	seconds, err := time.ParseDuration(raw + "s")
	if err == nil {
		return seconds
	}

	return time.Duration(fallbackSeconds) * time.Second
}

func envOr(key string, fallback string) string {
	if value := strings.TrimSpace(os.Getenv(key)); value != "" {
		return value
	}
	return fallback
}

func loadDotEnv() {
	startDir, err := os.Getwd()
	if err != nil {
		return
	}

	path := findDotEnv(startDir)
	if path == "" {
		return
	}

	content, err := os.ReadFile(path)
	if err != nil {
		return
	}

	lines := strings.Split(string(content), "\n")
	for _, rawLine := range lines {
		line := strings.TrimSpace(rawLine)
		if line == "" || strings.HasPrefix(line, "#") {
			continue
		}

		if strings.HasPrefix(line, "export ") {
			line = strings.TrimSpace(strings.TrimPrefix(line, "export "))
		}

		key, value, ok := strings.Cut(line, "=")
		if !ok {
			continue
		}

		key = strings.TrimSpace(key)
		value = strings.TrimSpace(value)
		if key == "" {
			continue
		}

		if len(value) >= 2 {
			if (value[0] == '"' && value[len(value)-1] == '"') || (value[0] == '\'' && value[len(value)-1] == '\'') {
				value = value[1 : len(value)-1]
			}
		}

		if _, exists := os.LookupEnv(key); !exists {
			os.Setenv(key, value)
		}
	}
}

func findDotEnv(start string) string {
	dir := start
	for {
		candidate := filepath.Join(dir, ".env")
		if _, err := os.Stat(candidate); err == nil {
			return candidate
		}

		parent := filepath.Dir(dir)
		if parent == dir {
			return ""
		}
		dir = parent
	}
}
