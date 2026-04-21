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

### Python (OpenAI Compatible)

```python
from openai import OpenAI

client = OpenAI(
    api_key="YOUR_API_KEY",
    base_url="https://api.imarouter.com/v1"
)

response = client.chat.completions.create(
    model="gpt-4o",
    messages=[{"role": "user", "content": "Hello!"}]
)

print(response.choices[0].message.content)
```

---

### Anthropic Compatible

```python
import anthropic

client = anthropic.Anthropic(
    api_key="YOUR_API_KEY",
    base_url="https://api.imarouter.com"
)

response = client.messages.create(
    model="claude-3",
    messages=[{"role": "user", "content": "Hello!"}]
)

print(response)
```

---

### cURL Example

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
https://open-route-api.fashionlabs.cn/431672322e0

---

## 🌍 Roadmap

* [ ] SDK (Python / Go / Java)
* [ ] Cost comparison dashboard
* [ ] Model benchmarks
* [ ] More multimodal demos

---

## 📄 License

MIT License

---

## ⭐ Support

If this project helps you, please ⭐ star the repo!
