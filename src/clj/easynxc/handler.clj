(ns easynxc.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [easynxc.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [easynxc.yt :as yt]
            [clojure.java.io :as io]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:rel "stylesheet" :href "http://yui.yahooapis.com/pure/0.6.0/pure-min.css"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(def loading-page
  (html5
   (head)
   [:body {:class "body-container"}
    [:div#app]
    (include-js "/js/app.js")]))

(defn song [url]
  (let [response (yt/download url)]
    (if (response :filename)
      {:status 200
       :headers {"Content-Type" "audio/mpeg"}
       :body (io/file (response :filename))}
      {:status 500
       :body (str (response :err))})))

(defroutes routes
  (GET "/songs/:url" [url] (song url))
  (GET "*" [] loading-page)
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
