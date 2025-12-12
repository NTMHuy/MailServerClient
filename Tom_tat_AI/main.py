#
import pickle
import numpy as np
from pyvi import ViTokenizer

#Load
#with open ('neg.pkl', 'rb') as fp:
#   contents = pickle.load(fp)

#Pre-processing
#contents_parsed = []
#for content in contents[0:10]:
#    contents_parsed.append(content.lower().strip())

#print(contents[3])

#print(contents_parsed[3])
text = input("Nhập đoạn văn cần tóm tắt:\n")
text = text.lower().strip()

import nltk
nltk.download('punkt_tab')

#sentences = nltk.sent_tokenize(contents_parsed[3])
sentences = nltk.sent_tokenize(text)
print(sentences)

from gensim.models import KeyedVectors 

#w2v = KeyedVectors.load_word2vec_format("cc.vi.300.vec", binary=False)

#w2v.save("cc.vi.300.kv")
# Lần sau dùng:
w2v = KeyedVectors.load("cc.vi.300.kv")

#print("✅ Đã load thành công!")
#print("Số lượng từ trong model:", len(w2v.key_to_index))
#print("Kích thước vector mỗi từ:", w2v.vector_size)

#vocab = w2v.wv.vocab
vocab = w2v.key_to_index


X = []
for sentence in sentences:
    sentence = ViTokenizer.tokenize(sentence)
    words = sentence.split(" ")
    sentence_vec = np.zeros((w2v.vector_size,))
    for word in words:
        if word in vocab:
            #sentence_vec+=w2v.wv[word]
            sentence_vec+=w2v[word]
    X.append(sentence_vec)

from sklearn.cluster import KMeans

n_clusters = 5
kmeans = KMeans(n_clusters=n_clusters)
kmeans = kmeans.fit(X)

from sklearn.metrics import pairwise_distances_argmin_min

avg = []
for j in range(n_clusters):
    idx = np.where(kmeans.labels_ == j)[0]
    avg.append(np.mean(idx))
closest, _ = pairwise_distances_argmin_min(kmeans.cluster_centers_, X)
ordering = sorted(range(n_clusters), key=lambda k: avg[k])
summary = ' '.join([sentences[closest[idx]] for idx in ordering])
print("\nTóm tắt nội dung:")
print(summary)

print("end")