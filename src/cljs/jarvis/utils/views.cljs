(ns jarvis.utils.views
  (:require
   [jarvis.utils.components :as c]
   [jarvis.utils.events :refer [<sub]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reitit.frontend.easy :as rfe]))


;;; ---------------------------------------------------------------------------
;;; Modal
;;; ---------------------------------------------------------------------------


(defn modal-ui
  "Component to display the currently active modal."
  []
  (when-let [modal @(rf/subscribe [:modal])]
    [modal]))


(defn error-modal-ui
  "Component to display the currently error (on a modal)."
  []
  (when-let [error-msg (<sub [:common/error])]
    [c/modal
     {:header
      "An error has occured"
      :body [c/pretty-display error-msg]
      :footer
      [:div
       [:button.btn.btn-sm.btn-danger
        {:on-click #(rf/dispatch [:common/set-error nil])}
        "Close"]]}]))


;;; ---------------------------------------------------------------------------
;;; Base
;;; ---------------------------------------------------------------------------


;;; ---------------------------------------------------------------------------
;;; Navbar 

;; TODO
(defn breadcrumbs []
  [:nav
   {:aria-label "breadcrumb"}
   [:ol
    {:class
     "breadcrumb bg-transparent mb-0 pb-0 pt-1 px-0 me-sm-6 me-5"}
    [:li
     {:class "breadcrumb-item text-sm"}
     [:a
      {:class "opacity-5 text-dark", :href ""}
      "Pages"]]
    [:li
     {:class "breadcrumb-item text-sm text-dark active",
      :aria-current "page"}
     "Dashboard"]]
   [:h6 {:class "font-weight-bolder mb-0"} "Dashboard"]])


;; TODO
(defn search-bar []
  [:div
   {:class "ms-md-auto pe-md-3 d-flex align-items-center"}
   [:div
    {:class "input-group input-group-outline"}
    [:label {:class "form-label"} "Type here..."]
    [:input {:type "text", :class "form-control"}]]])


(defn online-builder-button []
  [:li
   {:class "nav-item d-flex align-items-center"}
   [:a
    {:class "btn btn-outline-primary btn-sm mb-0 me-3",
     :target "_blank",
     :href
     "https://www.creative-tim.com/builder?ref=navbar-material-dashboard"}
    "Online Builder"]])

(defn github-stars-link []
  [:li
   {:class "mt-2"}
   [:a
    {:class "github-button",
     :href
     "https://github.com/creativetimofficial/material-dashboard",
     :data-icon "octicon-star",
     :data-size "large",
     :data-show-count "true",
     :aria-label
     "Star creativetimofficial/material-dashboard on GitHub"}
    "Star"]])

(defn sidebar-toggler []
  [:li
   {:class "nav-item d-xl-none ps-3 d-flex align-items-center"}
   [:a
    {:href "",
     :class "nav-link text-body p-0",
     :id "iconNavbarSidenav"}
    [:div
     {:class "sidenav-toggler-inner"}
     [:i {:class "sidenav-toggler-line"}]
     [:i {:class "sidenav-toggler-line"}]
     [:i {:class "sidenav-toggler-line"}]]]])

(defn settings-button []
  [:li
   {:class "nav-item px-3 d-flex align-items-center"}
   [:a
    {:href "", :class "nav-link text-body p-0"}
    [:i
     {:class "fa fa-cog fixed-plugin-button-nav cursor-pointer"}]]])

(defn notifications-widget []
  [:li
   {:class "nav-item dropdown pe-2 d-flex align-items-center"}
   [:a
    {:href "",
     :class "nav-link text-body p-0",
     :id "dropdownMenuButton",
     :data-bs-toggle "dropdown",
     :aria-expanded "false"}
    [:i {:class "fa fa-bell cursor-pointer"}]]

   ;; notification list
   [:ul
    {:class "dropdown-menu  dropdown-menu-end  px-2 py-3 me-sm-n4",
     :aria-labelledby "dropdownMenuButton"}
    [:li
     {:class "mb-2"}
     [:a
      {:class "dropdown-item border-radius-md",
       :href ""}
      [:div
       {:class "d-flex py-1"}
       [:div
        {:class "my-auto"}
        #_[:img
           {:src "../assets/img/team-2.jpg",
            :class "avatar avatar-sm  me-3"}]]
       [:div
        {:class "d-flex flex-column justify-content-center"}
        [:h6
         {:class "text-sm font-weight-normal mb-1"}
         [:span {:class "font-weight-bold"} "New message"]
         "from Laur"]
        [:p
         {:class "text-xs text-secondary mb-0"}
         [:i {:class "fa fa-clock me-1"}]
         "13 minutes ago"]]]]]
    [:li
     {:class "mb-2"}
     [:a
      {:class "dropdown-item border-radius-md",
       :href ""}
      [:div
       {:class "d-flex py-1"}
       [:div
        {:class "my-auto"}
        #_[:img
           {:src "../assets/img/small-logos/logo-spotify.svg",
            :class "avatar avatar-sm bg-gradient-dark  me-3"}]]
       [:div
        {:class "d-flex flex-column justify-content-center"}
        [:h6
         {:class "text-sm font-weight-normal mb-1"}
         [:span {:class "font-weight-bold"} "New album"]
         "by Travis Scott"]
        [:p
         {:class "text-xs text-secondary mb-0"}
         [:i {:class "fa fa-clock me-1"}]
         "1 day"]]]]]
    [:li
     [:a
      {:class "dropdown-item border-radius-md",
       :href ""}
      [:div
       {:class "d-flex py-1"}
       [:div
        {:class
         "avatar avatar-sm bg-gradient-secondary  me-3  my-auto"}
        [:svg
         {:width "12px",
          :height "12px",
          :viewBox "0 0 43 36",
          :version "1.1",
          :xmlns "http://www.w3.org/2000/svg",
          :xmlnsXlink "http://www.w3.org/1999/xlink"}
         [:title "credit-card"]
         [:g
          {:stroke "none",
           :stroke-width "1",
           :fill "none",
           :fill-rule "evenodd"}
          [:g
           {:transform "translate(-2169.000000, -745.000000)",
            :fill "#FFFFFF",
            :fill-rule "nonzero"}
           [:g
            {:transform "translate(1716.000000, 291.000000)"}
            [:g
             {:transform "translate(453.000000, 454.000000)"}
             [:path
              {:class "color-background",
               :d
               "M43,10.7482083 L43,3.58333333 C43,1.60354167 41.3964583,0 39.4166667,0 L3.58333333,0 C1.60354167,0 0,1.60354167 0,3.58333333 L0,10.7482083 L43,10.7482083 Z",
               :opacity "0.593633743"}]
             [:path
              {:class "color-background",
               :d
               "M0,16.125 L0,32.25 C0,34.2297917 1.60354167,35.8333333 3.58333333,35.8333333 L39.4166667,35.8333333 C41.3964583,35.8333333 43,34.2297917 43,32.25 L43,16.125 L0,16.125 Z M19.7083333,26.875 L7.16666667,26.875 L7.16666667,23.2916667 L19.7083333,23.2916667 L19.7083333,26.875 Z M35.8333333,26.875 L28.6666667,26.875 L28.6666667,23.2916667 L35.8333333,23.2916667 L35.8333333,26.875 Z"}]]]]]]]
       [:div
        {:class "d-flex flex-column justify-content-center"}
        [:h6
         {:class "text-sm font-weight-normal mb-1"}
         "Payment successfully completed"]
        [:p
         {:class "text-xs text-secondary mb-0"}
         [:i {:class "fa fa-clock me-1"}]
         "2 days"]]]]]]])

(defn sign-in-icon []
  [:li
   {:class "nav-item d-flex align-items-center"}
   [:a
    {:href "../pages/sign-in.html",
     :class "nav-link text-body font-weight-bold px-0"}
    [:i {:class "fa fa-user me-sm-1"}]
    [:span {:class "d-sm-inline d-none"} "Sign In"]]])


(defn navbar []
  [:nav
   {:class
    "navbar navbar-main navbar-expand-lg px-0 mx-4 shadow-none border-radius-xl",
    :id "navbarBlur",
    :data-scroll "true"}
   [:div
    {:class "container-fluid py-1 px-3"}

    ;; TODO
    [breadcrumbs]

    [:div
     {:class "collapse navbar-collapse mt-sm-0 mt-2 me-md-0 me-sm-4",
      :id "navbar"}

     ;; TODO
     [search-bar]

     [:ul
      {:class "navbar-nav  justify-content-end"}

      ;; TODO
      ;; online builder link (replace with ?)
      [online-builder-button]

      ;; TODO
      ;; star @ github (replace with ?)
      [github-stars-link]

      ;; sidebar toggler
      [sidebar-toggler]

      ;; TODO
      ;; settings
      [settings-button]

      ;; TODO
      ;; notifications
      [notifications-widget]

      ;; TODO
      ;; sign in link (replace with logout link/icon)
      [sign-in-icon]]]]])


;;; ---------------------------------------------------------------------------
;;; Sidebar 


(defn sidebar-logo []
  [:div
   {:class "sidenav-header"}
   [:i
    {:class
     "fas fa-times p-3 cursor-pointer text-white opacity-5 position-absolute end-0 top-0 d-none d-xl-none",
     :aria-hidden "true",
     :id "iconSidenav"}]
   [:a
    {:class "navbar-brand m-0",
     :href "https://virtuai.com"
     :target "_blank"}
    ;; TODO: add logo img
    #_[:img
       {:src "/img/logo-ct.png",
        :class "navbar-brand-img h-100",
        :alt "main_logo"}]
    [:span
     {:class "ms-1 font-weight-bold text-white"}
     "Jarvis AI"]]])


(defn nav-item [{:keys [icon href label]}]
  [:li
   {:class "nav-item"}
   [:a
    ; active bg-gradient-primary
    {:href href
     :class "nav-link text-white link-style"}
    icon
    [:span {:class "sidenav-normal  ms-2  ps-1"} label]]])


(defn new-project-tab []
  [:li
   {:class "nav-item"}
   [:a
    {:class "nav-link text-white link-style"}
    [:span {:class "sidenav-mini-icon"} "NP"]
    [:span {:class "nav-link-text ms-2 ps-1"} "New Project"]]])


(defn projects-tab []
  [:li
   {:class "nav-item"}

   [:a
    {:data-bs-toggle "collapse",
     :href "#projectsTab",
     :class "nav-link text-white",
     :aria-controls "projectsTab",
     :role "button"
     :aria-expanded "false"}
    [:i {:class "material-icons-round opacity-10"} "image"]
    [:span {:class "nav-link-text ms-2 ps-1"} "Projects"]]

   ;; Dropdown items
   [:div
    {:class "collapse", :id "projectsTab"} ; show
    [:ul
     {:class "nav"}

     [nav-item
      {:label "New Project"
       :icon [:i {:class "material-icons-round"} "add"]
       :href (rfe/href :projects/new)}]

     [nav-item
      {:icon [:span {:class "sidenav-mini-icon"} "SRP"]
       :label "Some Random Project"}]]]])


(defn sidebar-navigation []
  [:div
   {:class "collapse navbar-collapse  w-auto",
    :id "sidenav-collapse-main"}

   [:ul
    {:class "navbar-nav"}

    ;; Dashboard
    [:li
     {:class "nav-item"}
     [:a
      {:class "nav-link text-white",
       :href (rfe/href :home)}
      [:div
       {:class
        "text-white text-center me-2 d-flex align-items-center justify-content-center"}
       [:i {:class "material-icons opacity-10"} "dashboard"]]
      [:span {:class "nav-link-text ms-1"} "All Projects"]]]

    [projects-tab]

    ;; Profile
    [:li
     {:class "nav-item"}
     [:a
      {:class "nav-link text-white", :href "../pages/profile.html"}
      [:div
       {:class
        "text-white text-center me-2 d-flex align-items-center justify-content-center"}
       [:i {:class "material-icons opacity-10"} "person"]]
      [:span {:class "nav-link-text ms-1"} "Profile"]]]]])


(defn sidebar []
  [:aside
   {:class
    "sidenav navbar navbar-vertical navbar-expand-xs border-0 border-radius-xl my-3 fixed-start ms-3   bg-gradient-dark",
    :id "sidenav-main"}

   [sidebar-logo]

   [:hr {:class "horizontal light mt-0 mb-2"}]

   [sidebar-navigation]])


;;; ---------------------------------------------------------------------------
;;; Misc 


(defn metadata []
  [:meta {:charset "utf-8"}]
  [:meta
   {:name "viewport",
    :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
  [:link
   {:rel "apple-touch-icon",
    :sizes "76x76",
    :href "/img/apple-icon.png"}]
  [:link
   {:rel "icon", :type "image/png", :href "/img/favicon.png"}]
  [:title "Jarvis PA"])


; TODO
(defn analytics [])


(defn footer []
  [:footer
   {:class "footer py-4"}
   [:div
    {:class "container-fluid"}
    [:div
     {:class "row align-items-center justify-content-lg-between"}
     [:div
      {:class "col-lg-6 mb-lg-0 mb-4"}
      [:div
       {:class
        "copyright text-center text-sm text-muted text-lg-start"}
       " © " (.getFullYear (js/Date.)) ", made with "
       [:i {:class "fa fa-heart"}] " by "
       [:a
        {:href "https://www.laconiccrafts.com",
         :class "font-weight-bold",
         :target "_blank"}
        "Laconic Crafts"]
       " for a better web."]]
     [:div
      {:class "col-lg-6"}
      [:ul
       {:class
        "nav nav-footer justify-content-center justify-content-lg-end"}
       [:li
        {:class "nav-item"}
        [:a
         {:href "https://www.virtuai.com",
          :class "nav-link text-muted",
          :target "_blank"}
         "Jarvis AI"]]
       [:li
        {:class "nav-item"}
        [:a
         {:href "https://www.virtuai.com/about",
          :class "nav-link text-muted",
          :target "_blank"}
         "About Us"]]
       [:li
        {:class "nav-item"}
        [:a
         {:href "https://www.virtuai.com/blog",
          :class "nav-link text-muted",
          :target "_blank"}
         "Blog"]]
       [:li
        {:class "nav-item"}
        [:a
         {:href "https://www.virtuai.com/license",
          :class "nav-link pe-0 text-muted",
          :target "_blank"}
         "License"]]]]]]])


;;; ---------------------------------------------------------------------------
;;; Base 


(defn dashboard-base-ui [& components]
  (r/with-let [user (rf/subscribe [:identity])]
    [:div
     [metadata]
     [analytics]
     [:div
      [modal-ui]
      [error-modal-ui]
      [:div
       [sidebar]
       [:main
        {:class "main-content position-relative max-height-vh-100 h-100 border-radius-lg"}
        [navbar]
        (into
         [:div
          {:class "container-fluid py-4"}]
         components)
        [footer]]]]]))


;; TODO
(defn loggedout-base-ui [& components])