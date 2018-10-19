import csv
import itertools
import nltk
import numpy as np
# nltk.download('punkt')

vocabulary_size = 8000
unknown_token = "UNKNOWN_TOKEN"
sentence_start_token = "SENTENCE_START"
sentence_end_token = "SENTENCE_END"
# read the data and append _start, _end tokens
print("Read data")
with open('data/results-20181019-181736.csv', 'r') as f:
	reader = csv.reader(f, skipinitialspace=True)
	next(reader)
	sentences = itertools.chain(*[nltk.sent_tokenize(x[0].lower()) for x in reader])
	sentences = ["%s %s %s" % (sentence_start_token, x, sentence_end_token) for x in sentences]
	# print(sentences)
# print(len(sentences))

tokenized_sentences = [nltk.word_tokenize(sent) for sent in sentences]
# print(tokenized_sentences)
word_freq = nltk.FreqDist(itertools.chain(*tokenized_sentences))
# print(word_freq.items())

vocab = word_freq.most_common(vocabulary_size-1)
# print(type(vocab))
index_to_word = [x[0] for x in vocab]
index_to_word.append(unknown_token)
# print(index_to_word)
word_to_index = dict([(w,i) for i,w in enumerate(index_to_word)])
# print(word_to_index)
print(vocab[-1][0], vocab[-1][1])
for i, sent in enumerate(tokenized_sentences):
	tokenized_sentences[i] = [w if w in word_to_index else unknown_token for w in sent]

X_train = np.asarray([[word_to_index[w] for w in sent[:-1]] for sent in tokenized_sentences])
y_train = np.asarray([[word_to_index[w] for w in sent[1:]] for sent in tokenized_sentences])

print(X_train)