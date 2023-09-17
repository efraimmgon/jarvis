(ns jarvis.openai
  (:require
   [ajax.core :as ajax]
   [cljs.pprint]
   [jarvis.env :as env]
   [jarvis.utils.events :refer [base-interceptors]]
   [re-frame.core :as rf]))

;;; my code

(defn get-embedding [response]
  (-> response :data first :embedding))

(defn get-tokens [response]
  (-> response :data :embedding :usage :total_tokens))


(rf/reg-event-fx
 :write-embeddings
 base-interceptors
 (fn [_ [response]]
   (println "Tokens:" (get-tokens response))
   {:dispatch [:fsdb/create!
               {:tname :openai-embeddings
                :data (get-embedding response)}]}))


; max 8191 tokens
(rf/reg-event-fx
 :create-embeddings
 base-interceptors
 (fn [{:keys [_]} [{:keys [input on-success on-failure]}]]
   {:http-xhrio {:uri "https://api.openai.com/v1/embeddings"
                 :method :post
                 :headers {"Authorization" (str "Bearer " env/openai-api-key)}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :params {:input input
                          :model "text-embedding-ada-002"}
                 :on-success on-success
                 :on-failure (or on-failure [:common/log])}}))

;;; source
;;; https://knowlo.co/blog/building-a-proof-of-concept-using-openai-embeddings-to-make-ai-driven-tooltips/


;;; Creating Embeddings

;; TODO
(defn get-all-records
  "Get all helpdesk articles from the db."
  [])

;; TODO
(defn write-embeddings
  "Write the given embeddings to the db."
  [embeddings])


(defn create-embedding
  "Create an embedding using open ai api."
  [input]
  (js/Promise.
   (fn [resolve reject]
     (ajax/ajax-request
      {:method :post
       :uri "https://api.openai.com/v1/embeddings"
       :headers {"Authorization" (str "Bearer " env/openai-api-key)}
       :params {:input input
                :model "text-embedding-ada-002"}
       :response-format (ajax/json-response-format {:keywords? true})
       :format (ajax/json-request-format)
       :handler resolve
       :error-handler reject}))))


(defn embedding-main
  "Create embeddings for all helpdesk articles and write them to the db."
  []
  (let [records (get-all-records)
        output (->>
                records
                (mapv (fn [record]
                        (create-embedding record)))
                js/Promise.allSettled)
        successful (reduce (fn [acc ^js row]
                             (if (= (.-status row) "fulfilled")
                               (conj acc (.-value row))
                               (do (println "Error:" (.-reason row))
                                   acc)))
                           [] output)]
    ;; Calculate and log the # of tokens used in the process
    (println
     "Total usage:" (->>
                     successful
                     (mapv (fn [^js row]
                             (let [tokens (-> row .-embedding .-usage .-total_tokens)]
                               (println "Tokens:" tokens)
                               tokens)))
                     (reduce +)))
    ;; Write the output to the db
    (write-embeddings successful)))


;;; Answering Questions

;; TODO
(defn get-embeddings
  "Get all embeddings from the db."
  [])


(defn square [n]
  (* n n))

(defn sum-of-squares [v]
  (->> v
       (map square)
       (reduce +)))

(defn magnitude [v1 v2]
  (* (Math/sqrt (sum-of-squares v1))
     (Math/sqrt (sum-of-squares v2))))

(defn dot-product [v1 v2]
  (->> v1
       (map-indexed
        (fn [i x]
          (* x (get v2 i))))
       (reduce +)))

(defn cosine-similarity
  "Calculate the cosine similarity between the given vectors."
  [v1 v2]
  (/ (dot-product v1 v2)
     (magnitude v1 v2)))


(defn prompt [context question]
  (str
   "Generate a short answer to the question at the bottom of this text, in 
     context of the helpdesk article pasted below.
     The answer should be JSON  and it should contain the following fields:
     - 'answer' -> a short answer, up to 500 characters
     - 'article_slug' -> a slug for the knowledge base article
     - 'article_title' -> a title of the knowledge base artile
     If you don't have a certain answer, reply with the following JSON:
     {'error': 'ANSWER_NOT_AVAILABLE'
     
     Helpdesk article(s):\n"
   context
   "\nQuestion:\n"
   question))

(defn generate-answer
  "Generate an answer to the given question using the given helpdesk article 
   as context."
  [context question]
  (->
   (js/Promise.
    (ajax/ajax-request
     {:method :post
      :uri "https://api.openai.com/v1/chat/completions"
      :headers {"Authorization" (str "Bearer " env/openai-api-key)}
      :params {"model" "gpt-3.5-turbo"
               "messages" [{"role" "user"
                            "content" (prompt context question)}]
               "temperature" 0.5}}))


   (.then
    (fn [^js response]
      (-> response .-choices first .-message .-content .trim)))))


(defn most-similar-article-index
  "Get the index of the article with the highest similarity score."
  [similarities]
  (->>
   similarities
   (map-indexed vector)
   (reduce (fn [max-idx [idx curr]]
             (if (> (:similarity curr) (:similarity (similarities max-idx)))
               idx
               max-idx))
           0)))


(defn gen-similarities
  "Calculate the cosine similarity between the given question embedding and 
   each helpdesk article embedding."
  [^js question-embedding embeddings]
  (->>
   embeddings
   (map-indexed
    (fn [i ^js item]
      {:index i
       :similarity (cosine-similarity ; (2)
                    (-> question-embedding .-data first .-embedding)
                    (-> item .-data first .-embedding))}))))


;; (1) Create an embedding from the question.
;; (2) Calculate the cosine similarity between the question embedding and each 
;; helpdesk article embeddings.
;; (3) Choose the article with the highest similarity score.
;; (4) Use the GPT API to generate an answer using the selected article’s 
;; content as context.


(defn process-user-question
  "Process the given question and return an answer."
  [question]
  (let [embeddings (get-embeddings)
        qe (create-embedding question) ; (1)
        similarities (gen-similarities qe embeddings) ; (2)
        msa-idx (most-similar-article-index similarities)
        article (get embeddings msa-idx) ; (3)
        answer (generate-answer ; (4)
                (clj->js
                 {:id (.-id article)
                  :title (.-title article)
                  :slug (.-slug article)
                  :content (.-content article)})
                question)]
    (println "Tokens:" (-> qe .-embedding .-usage .-total_tokens))
    answer))
