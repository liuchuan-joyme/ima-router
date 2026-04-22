# ima-router

- 🔥 One API Key for ALL AI models
- 🎬 LLM + Image + Video + Audio in ONE API
- 🌏 Mainstream China + US model coverage
- 💰 Better pricing, stronger stability, faster model access
- 🔁 OpenAI + Anthropic compatible

👉 Stop switching between APIs. Use everything with one key.

---

## 🚀 What is ima-router?

ima-router is a **unified AI gateway** that lets you:

- 👉 Use **mainstream AI models across China + US with ONE API key**
- 👉 Access **LLMs + Video + Image + Audio** in a single API layer
- 👉 Switch providers and hot models without rewriting your integration
- 👉 Build on **pricing advantages, routing stability, and broad multimodal coverage**

---

## 🧠 What problem does ima-router solve?

AI teams today often need to:

* manage multiple API keys
* switch between different SDKs and provider formats
* integrate LLM, image, video, and audio APIs separately
* keep updating integrations when hot models change

👉 ima-router solves this with one unified API layer.

---

## ⚡ Why ima-router?

### 🏆 Built For Real Multimodal API Usage

ima-router is not just an LLM router.
It is designed for teams that need one production gateway for:

* LLM APIs
* image generation APIs
* video generation APIs
* audio and multimodal workflows

👉 Instead of stitching together separate providers, billing systems, and unstable adapters, you integrate once and expand from there.

---

### 🔑 One API Key

Instead of managing:

* one OpenAI key
* one Anthropic key
* one image provider account
* multiple video model providers
* separate billing and routing logic

👉 You only need **ONE API key**.

With ima-router, you get:

* one key for a fast-growing set of leading AI providers and model platforms
* one integration surface for LLM, Image, Video and Audio
* one billing and routing layer for your application stack

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

### 🌏 Broad Model Coverage

ima-router focuses on **broad multimodal coverage across mainstream China + US model ecosystems**.

That means you can use one gateway to reach:

* top frontier LLMs
* image models
* video generation models
* fast-moving new releases

👉 For the latest provider and model coverage, visit:
[www.imarouter.com](https://www.imarouter.com/)

---

### 🔥 Latest Hot Models

This section is intentionally kept as a **dynamic traffic entry** for the newest models we support, and we will keep updating it as new launches go live.

Current highlights:

* Claude 4.7
* Seedance 2.0
* GPT Image 2
* Banana
* Happy Horse
* Kling v3

👉 If the model or platform you need is not currently available, contact us and we can usually get it configured within **24 hours**.

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

👉 All accessible through one unified interface. For the latest model coverage, visit [www.imarouter.com](https://www.imarouter.com/)

---

### 🛡 Stability & Routing

* Unified routing layer for multiple providers and model families
* Easier failover and switching between providers
* Better operational consistency than stitching together many separate APIs

👉 Built for teams that care about production reliability, not just quick demos.

---

### 🤝 Developer Trust

* Microsoft official partner
* Built for production-style routing, not single-provider lock-in
* Designed for teams that care about reliability, coverage, and long-term integration stability

👉 ima-router is built to be a dependable infrastructure layer for AI products.

---

### 💰 Pricing Advantage

* Competitive pricing across multimodal workloads
* **0% markup**
* Transparent pricing strategy

👉 For the latest pricing and supported plans, visit [www.imarouter.com](https://www.imarouter.com/)

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

- 👉 Automatically selects the best model
- 👉 Optimizes for cost & performance

---

## 🧩 Architecture

ima-router provides:

* Multi-provider aggregation
* Intelligent routing
* Cost optimization
* Automatic failover
* Multimodal API unification

---

## 📚 Documentation

- 👉 Official website:
  [www.imarouter.com](https://www.imarouter.com/)

- 👉 Full API reference:
  [open-route-api.fashionlabs.cn/431672322e0](https://open-route-api.fashionlabs.cn/431672322e0)

- 👉 SDK-specific quick starts:

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

## 📬 Contact

* Business / partnerships: [bd@imarouter.com](mailto:bd@imarouter.com)
* User support: [support@imarouter.com](mailto:support@imarouter.com)

---

## ⭐ Support

If this project helps you, please ⭐ star the repo!
