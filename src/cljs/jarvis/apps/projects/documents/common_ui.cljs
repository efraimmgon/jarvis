(ns jarvis.apps.projects.documents.common-ui 
  (:require
    [jarvis.utils.input :as input]
    [jarvis.utils.views :as views]
    [re-frame.core :as rf]
    [reitit.frontend.easy :as rfe]))


(defn document-ui [document]
  (let [{:keys [id content name project-id]} @document]
    [views/dashboard-base-ui
     [:div
      {:class "container-fluid py-4"}
      [:div
       {:class "row mt-4"}
       [:div
        {:class "col-lg-9 col-12 mx-auto position-relative"}
        [:div
         {:class "card"}

         ; card header
         [:div
          {:class "card-header p-3 pt-2"}
          [:div
           {:class
            "icon icon-lg icon-shape bg-gradient-dark shadow text-center border-radius-xl mt-n4 me-3 float-start"}
           [:i {:class "material-icons opacity-10"} "event"]]
          [:h6 {:class "mb-0"}
           (if id
             name
             "New Document")
           (when id
             ; button on the right
             [:button.btn.btn-danger.ms-2.float-end
              {:on-click #(rf/dispatch [:projects.documents/delete! @document])}
              [:i.material-icons "delete_forever"]])]]


         ; card body
         [:div
          {:class "card-body pt-2"}

          ; name
          [:div
           {:class "input-group input-group-dynamic is-filled"}
           [:label
            {:for "documentName", :class "form-label"}
            "Document Name"]
           [input/text-input
            {:doc document,
             :name :name,
             :class "form-control",
             :id "documentName"}]]


          ; Content
          [:label {:class "mt-4"} "Document Content"]

          ; editor
          [input/rich-text-editor
           {:doc document,
            :name :content}]

          ; control buttons
          [:div
           {:class "d-flex justify-content-end mt-4"}

           [:a {:href (rfe/href :projects.documents/list
                        {:project-id project-id})
                :class "btn btn-light m-0"}
            "Cancel"]

           (if id
             [:button
              {:on-click #(-> ^js content
                            .save
                            (.then
                              (fn [output-data]
                                (rf/dispatch
                                  [:projects.documents/update!
                                   (assoc @document :content output-data)]))))
               :class "btn bg-gradient-dark m-0 ms-2"}
              "Update"]
             [:button
              {:on-click #(-> ^js content
                            .save
                            (.then
                              (fn [output-data]
                                (rf/dispatch
                                  [:projects.documents/create!
                                   (assoc @document :content output-data)]))))


               :class "btn bg-gradient-dark m-0 ms-2"}
              "Create"])]]]]]]]))