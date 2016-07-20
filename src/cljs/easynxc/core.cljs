(ns easynxc.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [clojure.string :as str]
            [easynxc.player :refer [load-audio]]
            [cemerick.url :as url]
            [cljs.core.async :as async :refer [<! >! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;; -------------------------
;; Views

(defn home-page []
  [:div {:class "player"} ""])

;; "► ❙❙ ↻"

(defn auto-play []
  (let [song-url (session/get :full-url)]
    [:div {:class "player"} song-url]))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Functions

(defn build-url [base params]
  (-> (url/url base)
      (assoc :query params)
      str))

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/*" {base :* params :query-params}
  (go
    (let [audio (<! (load-audio (str "/songs/" (url/url-encode (build-url base params)))))]
      (.start audio 0)
      (set! (.-value (.-playbackRate audio)) 0.5)))
  (session/put! :current-page #'auto-play))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (secretary/dispatch! path))
    :path-exists?
    (fn [path]
      (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
