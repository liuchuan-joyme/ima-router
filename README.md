# ima-router

🔥 One API Key for ALL AI models
🧠 OpenAI + Anthropic API compatible
🎬 Unified access to Video, Image, Audio & LLM
💰 Save up to 66% vs fal.ai

---

## 🚀 What is ima-router?

ima-router is a **unified AI gateway** that lets you:

👉 Use **all major AI models with ONE API key**
👉 Access **LLMs + Video + Image + Audio** in a single API
👉 Switch providers without changing your code

---

## ⚡ Why ima-router?

### 🔑 One API Key

No more managing multiple providers or constantly updating integrations one by one.

* One key for a fast-growing set of leading AI providers
* One unified integration surface for LLM, Image, Video and Audio
* One billing and routing layer for your app

👉 For the latest supported providers and models, visit our official website:
[www.imarouter.com](https://www.imarouter.com/)

---

### 🔁 Multi-API Compatibility

ima-router supports industry-standard APIs:

* ✅ OpenAI Chat Completions API
* ✅ OpenAI Responses API
* ✅ Anthropic Messages API

👉 Zero migration cost for existing apps

---

### 🔥 Latest Hot Models

This section is intentionally kept as a **dynamic traffic entry** for the newest models we support.
We will keep updating it as new launches go live.

Current highlights:

* Claude 4.7
* Seedance 2.0
* GPT Image 2
* Banana
* Happy Horse
* Kling v3

👉 For the latest supported hot models and continuously updated lineup, visit:
[www.imarouter.com](https://www.imarouter.com/)

---

### 🧰 SDKs & Examples

Use the language-specific guides instead of putting every Quick Start directly on the repository front page:

* SDK index: [sdk/README.md](sdk/README.md)
* Examples index: [examples/README.md](examples/README.md)
* Python SDK: [sdk/python/README.md](sdk/python/README.md)
* Go SDK: [sdk/go/README.md](sdk/go/README.md)
* Java SDK: [sdk/java/README.md](sdk/java/README.md)
* Python examples: [examples/python/README.md](examples/python/README.md)
* Go examples: [examples/go/README.md](examples/go/README.md)
* Java examples: [examples/java/README.md](examples/java/README.md)

---

### 🎬 Full Multimodal Support

Representative examples:

#### 🧠 LLMs

* Claude
* GPT
* Kimi
* Doubao
* and more

#### 🎥 Video Generation

* Seedance 2.0
* Vidu
* Kling
* MiniMax
* PixVerse
* and more

#### 🖼 Image Generation

* Gemini
* Midjourney (MJ)
* Banana
* Happy Horse
* and more

👉 All accessible through one unified interface.
For the latest model coverage, visit [www.imarouter.com](https://www.imarouter.com/)

---

### 💰 Lower Cost

* Up to **66% cheaper** than fal.ai
* **0% markup**
* Transparent pricing

---

### ⚡ Fast Model Integration

* 🔥 Hot models → within **24 hours**
* ⭐ Major models → within **3 days**

---

## 🚀 Quick Start

The repository root should stay concise. For language-specific setup, SDK usage, and runnable demos, use the links above.

Minimal universal example:

```bash
curl https://api.imarouter.com/v1/chat/completions \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-4o",
    "messages": [{"role": "user", "content": "Hello!"}]
  }'
```

---

## 🧠 Smart Routing (Auto Mode)

```python
response = client.chat.completions.create(
    model="auto",
    messages=[{"role": "user", "content": "Write a product description"}]
)
```

👉 Automatically selects the best model
👉 Optimizes for cost & performance

---

## 🧩 Architecture

ima-router provides:

* Multi-provider aggregation
* Intelligent routing
* Cost optimization
* Automatic failover

---

## 📚 Documentation

👉 Full API reference:
[open-route-api.fashionlabs.cn/431672322e0](https://open-route-api.fashionlabs.cn/431672322e0)

👉 SDK-specific quick starts:

* [Python SDK](sdk/python/README.md)
* [Go SDK](sdk/go/README.md)
* [Java SDK](sdk/java/README.md)

---

## 🌍 Roadmap

* [ ] Cost comparison dashboard
* [ ] Model benchmarks
* [ ] More multimodal demos

---

## 📄 License

MIT License

---

## ⭐ Support

If this project helps you, please ⭐ star the repo!
