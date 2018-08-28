(ns sand-compass.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [sand-compass.ajax :refer [load-interceptors!]]
            [sand-compass.handlers]
            [sand-compass.subscriptions]
            [sand-compass.views.menusnavs :as navs])
  (:import goog.History))

; (defn nav-link [uri title page collapsed?]
;   (let [selected-page (rf/subscribe [:page])]
;     [:li.nav-item
;      {:class (when (= page @selected-page) "active")}
;      [:a.nav-link
;       {:href uri
;        :on-click #(reset! collapsed? true)} title]]))

(defn about-page []
  [:div.content-wrapper
    [:section.content-header
      [:h1 "sand-compass..."
        [:small "work in progress"]]
      [:ol.breadcrumb
        [:li
          [:a {:href "#"}
            [:i.fa.fa-dashboard]
            "Level"]]
        [:li.active "Here"]]]])

(defn home-page []
  [:div.content-wrapper

    [:section.content-header
      [:ol.breadcrumb
        [:li
          [:a {:href "#"}
            [:i.fa.fa-dashboard]
            "sand-compass"]]
        [:li.active "Home"]]
      [:h1 "Home"]]

    [:section.content
      [:p "Welcome to Sand Compass"]]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [:div.wrapper
   [navs/main-header]
  ;  [navs/main-sidebar]
   [(pages @(rf/subscribe [:page]))]
   [navs/main-footer]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  ; (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
