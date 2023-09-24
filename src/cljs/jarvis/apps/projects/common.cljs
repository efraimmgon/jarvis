(ns jarvis.apps.projects.common
  (:require [jarvis.utils.input :as input]
            [jarvis.utils.views :as views]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]))


(defn project-ui [project]
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
         (if (:id @project)
           (:name @project)
           "New Project")]]


       ; card body
       [:div
        {:class "card-body pt-2"}

        ; project name
        [:div
         {:class "input-group input-group-dynamic is-filled"}
         [:label
          {:for "projectName", :class "form-label"}
          "Project Name"]
         [input/text-input
          {:doc project,
           :name :name,
           :class "form-control",
           :id "projectName"}]]

        ; Boost Project
        [:div
         {:class "row mt-4"}
         [:div
          {:class "col-12 col-md-6"}
          [:div
           {:class "form-group"}
           [:label "Boost Project"]
           [:p
            {:class "form-text text-muted ms-1"}
            "Boosted projects are promoted at the sidebar, instead of 
                   being shown only in the projects tab."]

           [:div
            {:class "form-check form-switch ms-1"}
            [:input
             {:type :checkbox
              :class "form-check-input"
              :on-change #(swap! project update :boosted? not)
              :checked (:boosted? @project)}]]]]]

        ; Project description
        [:label {:class "mt-4"} "Project Description"]

        ; editor
        [input/rich-text-editor
         {:doc project,
          :name :description}]

        ; control buttons
        [:div
         {:class "d-flex justify-content-end mt-4"}

         [:a {:href (rfe/href :home)
              :class "btn btn-light m-0"}
          "Cancel"]

         (if (:id @project)
           [:button
            {:on-click #(rf/dispatch [:projects/update! project])
             :class "btn bg-gradient-dark m-0 ms-2"}
            "Update Project"]
           [:button
            {:on-click #(rf/dispatch [:projects/create! project])
             :class "btn bg-gradient-dark m-0 ms-2"}
            "Create Project"])]]]]]]])
