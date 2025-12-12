from flask import Flask, request, jsonify
from gensim.models import KeyedVectors
from pyvi import ViTokenizer
import numpy as np
import nltk
from sklearn.cluster import KMeans
from sklearn.metrics import pairwise_distances_argmin_min
import json
from flask import Response

app = Flask(__name__)

# 🔥 Load Word2Vec 1 lần duy nhất
print("Loading Word2Vec...")
w2v = KeyedVectors.load("cc.vi.300.kv", mmap='r')
vocab = w2v.key_to_index
print("Model loaded!")


@app.route("/summary", methods=["POST"])
def summarize():
    data = request.json
    text = data.get("text", "").lower().strip()

    sentences = nltk.sent_tokenize(text)
    if len(sentences) == 0:
        return jsonify({"summary": ""})

    X = []
    for sentence in sentences:
        sentence = ViTokenizer.tokenize(sentence)
        words = sentence.split(" ")
        sentence_vec = np.zeros((w2v.vector_size,))
        for word in words:
            if word in vocab:
                sentence_vec += w2v[word]
        X.append(sentence_vec)

    n_clusters = min(5, len(X))
    kmeans = KMeans(n_clusters=n_clusters).fit(X)

    avg = []
    for j in range(n_clusters):
        idx = np.where(kmeans.labels_ == j)[0]
        avg.append(np.mean(idx))

    closest, _ = pairwise_distances_argmin_min(kmeans.cluster_centers_, X)
    ordering = sorted(range(n_clusters), key=lambda k: avg[k])
    summary = ' '.join([sentences[closest[idx]] for idx in ordering])

    body = json.dumps({"summary": summary}, ensure_ascii=False)
    return Response(body, content_type="application/json; charset=utf-8")

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000)
