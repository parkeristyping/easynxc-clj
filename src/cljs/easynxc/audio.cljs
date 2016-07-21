(ns easynxc.audio
  (:require [goog.net.XhrIo]
            [cljs.core.async :as async :refer [<! >! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn decode-audio-data [context data]
  (let [ch (chan)]
    (.decodeAudioData context
                      data
                      (fn [buffer]
                        (go (>! ch buffer)
                            (close! ch))))
    ch))

(defn get-audio [url]
  (let [ch (chan)]
    (doto (goog.net.XhrIo.)
      (.setResponseType "arraybuffer")
      (.addEventListener goog.net.EventType.COMPLETE
                         (fn [event]
                           (let [res (-> event .-target .getResponse)]
                             (go (>! ch res)
                                 (close! ch)))))
      (.send url "GET"))
    ch))

(defn load-audio [url]
  (let [ch (chan)]
    (go
      (let [response (<! (get-audio url))
            AudioContext (or (.-AudioContext js/window)
                             (.-webkitAudioContext js/window))
            context (AudioContext.)
            buffer (<! (decode-audio-data context response))
            source (doto (.createBufferSource context)
                     (aset "buffer" buffer))]
        (.connect source (.-destination context))
        (>! ch {:source source :playing? false})
        (close! ch)))
    ch))

(defn start [{:keys [source playing?]}]
  (if (not playing?)
    (do (.start source 0)
        {:source source :playing? true})
    (let [context (.-context source)
          buffer (.-buffer source)
          new-source (doto (.createBufferSource context)
                       (aset "buffer" buffer))]
      (.stop source)
      (.connect new-source (.-destination context))
      (.start new-source 0)
      {:source new-source :playing? true})))
