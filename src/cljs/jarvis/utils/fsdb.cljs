(ns jarvis.utils.fsdb
  (:require
   [ajax.core :as ajax]
   [re-frame.core :as rf]
   [jarvis.utils.events :refer [base-interceptors]]))


(defn get-by-id
  [{:keys [coll id] :as params}]
  {:fun :get-by-id
   :params params})


(defn get-all
  [{:keys [coll] :as params}]
  {:fun :get-all
   :params params})


(defn select
  [{:keys [coll where order-by offset limit] :as params}]
  {:fun :select
   :params params})


(defn create!
  [{:keys [coll data] :as params}]
  {:fun :create!
   :params params})


(defn create-raw!
  [{:keys [coll data] :as params}]
  {:fun :create-raw!
   :params params})


(defn update!
  [{:keys [coll where data opts] :as params}]
  {:fun :update!
   :params params})


(defn upsert!
  [{:keys [coll where data opts] :as params}]
  {:fun :upsert!
   :params params})


(defn delete!
  [{:keys [coll id] :as params}]
  {:fun :delete!
   :params params})

(defn delete-coll!
  [{:keys [coll] :as params}]
  {:fun :delete-coll!
   :params params})


(rf/reg-event-fx
 :fsdb/query
 base-interceptors
 (fn [_ [{:keys [params on-success on-failure]}]]
   {:http-xhrio {:method :post
                 :uri "/api/fsdb"
                 :params params
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success (or on-success [:common/log])
                 :on-failure (or on-failure [:common/log])}}))

