(ns easynxc.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [easynxc.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [clojure.java.io :as io]
            [easynxc.yt :as yt]))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/normalize/4.2.0/normalize.min.css"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])


(def home-page
  (html5
   (head)
   [:body
    [:div#app
     [:p "this is a site for changing the speed of songs"]
     [:p "find a song on youtube or soundcloud or something and add \"easynxc.com/\" to the beginning of the URL"]
     [:p "example: " [:a {:href "/https://www.youtube.com/watch?v=5GL9JoH4Sws"} "easynxc.com/https://www.youtube.com/watch?v=5GL9JoH4Sws"]]
     [:p [:a {:href "https://github.com/parkeristyping/easynxc-clj"} "github"]]]
    (include-js "/js/google-analytics.js")]))

(def loading-page
  (html5
   (head)
   [:body
    [:div#app]
    (include-js "/js/app.js" "js/google-analytics.js")]))

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
  (GET "/" [] home-page)
  (GET "*" [] loading-page)
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
