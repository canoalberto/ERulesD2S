# Evolving Rule-Based Classifiers with Genetic Programming on GPUs for Drifting Data Streams

Designing efficient algorithms for mining massive high-speed data streams has become one of the contemporary challenges for the machine learning community. Such models must display highest possible accuracy and ability to swiftly adapt to any kind of changes, while at the same time being characterized by low time and memory complexities. However, little attention has been paid to designing learning systems that will allow us to gain a better understanding of incoming data. There are few proposals on how to design interpretable classifiers for drifting data streams, yet most of them are characterized by a significant trade-off between accuracy and interpretability. In this paper, we show that it is possible to have all of these desirable properties in one model. We introduce ERulesD2S: Evolving Rule-based classifier for Drifting Data Streams. By using grammar-guided genetic programming, we are able to obtain accurate sets of rules per class that are able to adapt to changes in the stream without a need for an explicit drift detector. Additionally, we augment our learning model with new proposals for rule propagation and data stream sampling, in order to maintain a balance between learning and forgetting of concepts. To improve efficiency of mining massive and non-stationary data, we implement ERulesD2S parallelized on GPUs. A thorough experimental study on 30 datasets proves that ERulesD2S is able to efficiently adapt to any type of concept drift and outperform state-of-the-art rule-based classifiers, while using small number of rules. At the same time ERulesD2S is highly competitive to other single and ensemble learners in terms of accuracy and computational complexity, while offering fully interpretable classification rules. Additionally, we show that ERulesD2S can scale-up efficiently to high-dimensional data streams, while offering very fast update and classification times. Finally, we present the learning capabilities of ERulesD2S for sparsely labeled data streams.

# Manuscript - Pattern Recognition

https://www.sciencedirect.com/science/article/abs/pii/S0031320318303765

# Citing ERulesD2S

> A. Cano and B. Krawczyk. Evolving Rule-Based Classifiers with Genetic Programming on GPUs for Drifting Data Streams. Pattern Recognition, vol. 87, 248-268, 2019.
