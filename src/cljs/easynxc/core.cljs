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
            [cljs.core.async :as async
             :refer [put! <! >! chan close! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

;; States

(defonce loading-status (atom ""))
(defonce active-audio (atom {}))

;; Views

(defn loading []
  [:div {:class "loading"} @loading-status])

(defn loaded []
  [:div {:class "big-button"}
   [:span {:class "clickable" :on-click start-audio} "►"]])

(defn playing []
  (let [update-speed (speed-controller (speed-updater))]
    [:div
     [:div {:class "mini-controls"}
      [:span {:class "mini-control-button" :on-click restart-audio} "↻"]]
     [:div {:on-mouse-move update-speed
            :on-click (fn [e] (do (update-speed e) (lock-speed e)))
            :class (str "big-center-text "
                        (if (@active-audio :locked?) "venusaur" "ivysaur"))}
      (gstring/format "%.2f" (@active-audio :speed))]]))

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

(defn start-loading [msg]
  (let [kill-ch (chan)]
    (go-loop []
      (reset! loading-status (subs msg 0 (mod (inc (count @loading-status)) (inc (count msg)))))
      (let [timeout-ch (timeout 1000)]
        (alt!
          timeout-ch (recur)
          kill-ch :done)))
    kill-ch))

(defn load-audio [loading-chan]
  (go
    (let [audio (<! (audio/load-audio @active-audio))]
      (reset! active-audio audio)
      (put! loading-chan :stop)
      (session/put! :current-page #'loaded))))

(defn start-audio [_]
  (swap! active-audio audio/start)
  (session/put! :current-page #'playing))

(defn restart-audio [_]
  (swap! active-audio audio/start))

(defn speed-controller [speed-ch]
  (fn [e]
    (let [x (.-clientX e)
          half-width (/ (.-innerWidth js/window) 2)]
      (put! speed-ch (/ x half-width)))))

(defn speed-updater []
  (let [speed-ch (chan)]
    (go-loop []
      (let [speed (<! speed-ch)]
        (if (not (@active-audio :locked?))
          (do
            (swap! active-audio #(assoc % :speed speed))
            (set! (.-value (.-playbackRate (@active-audio :source))) speed))))
      (recur))
    speed-ch))

(defn lock-speed [_]
  (update-params! "nxc" (gstring/format "%.3f" (@active-audio :speed)))
  (swap! active-audio #(assoc % :locked? (not (@active-audio :locked?)))))

(defn update-params! [k v]
  (let [url (url/url js/window.location.search)
        nxc ((:query url) k)
        new-url (if nxc
                  (update-in url [:query] dissoc k)
                  (assoc-in url [:query k] v))
        query (str/replace (str new-url) #"^://" "")]
    (.pushState js/window.history nil nil query)))

;; -------------------------
;; Routes

(secretary/defroute "/*" {base :* params :query-params}
  (reset! active-audio
          {:url (song-url base (dissoc params :nxc))
           :playing? false
           :speed (or (params :nxc) 1)
           :locked? (some? (params :nxc))
           :source nil})
  (load-audio (start-loading "LOADING"))
  (session/put! :current-page #'loading))

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
