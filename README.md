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

No more managing multiple providers:

* OpenAI
* Anthropic
* Google
* MiniMax
* Kling / Vidu / PixVerse

👉 Just **one key** to access everything

---

### 🔁 Multi-API Compatibility

ima-router supports industry-standard APIs:

* ✅ OpenAI Chat Completions API
* ✅ OpenAI Responses API
* ✅ Anthropic Messages API

👉 Zero migration cost for existing apps

---

### 🧰 SDKs & Examples

Use the language-specific guides instead of putting every Quick Start directly on the repository front page:

* SDK index: [sdk/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/sdk/README.md)
* Examples index: [examples/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/examples/README.md)
* Python SDK: [sdk/python/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/sdk/python/README.md)
* Go SDK: [sdk/go/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/sdk/go/README.md)
* Java SDK: [sdk/java/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/sdk/java/README.md)
* Python examples: [examples/python/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/examples/python/README.md)
* Go examples: [examples/go/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/examples/go/README.md)
* Java examples: [examples/java/README.md](/Users/liuchuan/Desktop/goProjects/ima-new/ima-router/examples/java/README.md)

---

### 🎬 Full Multimodal Support

#### 🧠 LLMs

* Claude
* GPT
* Kimi
* Doubao

#### 🎥 Video Generation

* Seedance 2.0
* Vidu
* Kling
* MiniMax
* PixVerse

#### 🖼 Image Generation

* Gemini
* Midjourney (MJ)

👉 All accessible through one unified interface

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
