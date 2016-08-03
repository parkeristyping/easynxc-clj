(ns easynxc.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [clojure.string :as str]
            [easynxc.audio :as audio]
            [cemerick.url :as url]
            [goog.string :as gstring]
            [goog.string.format]
            [cljs.core.async :as async :refer [put! <! >! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

;; -------------------------
;; Views

(defn home []
  [:div {:class "main"}
   [:p "hello and welcome to easynxc"]
   [:p "to use the site find a video from youtube or soundcloud or something and add easynxc.com/ to the beginning of it"]
   [:p "so if for example you wanted to try it with"]
   [:p [:a {:href "https://www.youtube.com/watch?v=5GL9JoH4Sws"} "https://www.youtube.com/watch?v=5GL9JoH4Sws"]]
   [:p "you'd navigate on over to..."]
   [:p [:a {:href "/https://www.youtube.com/watch?v=5GL9JoH4Sws"} "easynxc.com/https://www.youtube.com/watch?v=5GL9JoH4Sws"]]])

(defn loading-screen []
  [:div {:class "main"}
   [:div {:class "player"}
    [:span {:class "loading-msg"} @loading]]])

(defn player []
  [:div {:class "main" :on-mouse-move mouse-handler}
   [:div {:class "player"}
    [:span {:class "play-button" :on-click play-button-handler} @play-button]]
   [:div {:class "speed"}
    [:span (gstring/format "%.2f" (or (@active-audio :speed) 1))]]])

(defn current-page
  "Gets current page from the session"
  []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Functions and stuff

(defn song-url
  "Generate URL for AJAX call to load song"
  [base params]
  (let [raw-url (-> (url/url base)
                    (assoc :query params)
                    str)]
    (str "/songs/" (url/url-encode raw-url))))

(defonce loading (atom ""))

(defonce loading-updater
  (let [msg "LOADING"]
    (js/setInterval #(reset! loading (subs msg 0 (mod (inc (count @loading)) (inc (count msg))))) 1000)))

(def speed-chan (chan))

(defn mouse-handler
  "Parse only argument, a mouse move event, and pass to channel"
  [e]
  (let [x (.-clientX e)
        half-width (/ (.-innerWidth js/window) 2)]
    (put! speed-chan (/ x half-width))))

(go-loop []
  (let [speed (<! speed-chan)]
    (reset! active-audio (assoc @active-audio :speed speed))
    (set! (.-value (.-playbackRate (@active-audio :source))) speed))
  (recur))

(defonce active-audio (atom {:source nil :playing? false :speed 1}))

(defn load-song
  "Fetch song data. When received, update active-audio atom
   and load player page."
  [url]
  (go
    (let [audio (<! (audio/load-audio url))]
      (reset! active-audio audio)
      (session/put! :current-page #'player))))

(defonce play-button (atom "►"))
(defn play-button-handler
  "Takes any event as argument. Starts playing audio and
   replaces play-button with restart button."
  [_]
  (swap! active-audio audio/start)
  (reset! play-button "↻"))

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home))

(secretary/defroute "/*" {base :* params :query-params}
  (load-song (song-url base params))
  (session/put! :current-page #'loading-screen))

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
