(ns jarvis.apps.projects.documents.edit-document 
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    [jarvis.apps.projects.documents.common-ui :refer [document-ui]]))


(defn edit-document-ui []
  (let [document- (rf/subscribe [:projects.documents/active])]

    (set! (.-title js/document) "Edit Document")

    (fn []
      (when @document-
        (let [document (r/atom @document-)]
          [document-ui document])))))
