(ns easynxc.async-examples
  (:require [goog.net.XhrIo]
            [cljs.core.async :as async :refer [<! >! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))
;; -------------------------
;; Async examples

;; (go-loop []
;;   (js/alert (pr-str (<! cha)))
;;   (recur))

;; (def play [song-url]
;;   (let [audio-chan (chan)
;;         audio-ctx (js/AudioContext.)
;;         source (.createBufferSource audio-ctx)]
;;     (.decodeAudioData audio-ctx (go (<! (load-audio song-url))) #(go (>! audio-chan %)))
;;     (.buffer source (go (<! audio-chan)))
;;     (.connect source (.destination audio-ctx))
;;     (.start source 0)

;;     (go (<! audio-chan))

;;     (.decodeAudioData ac (go (<! song-channel)) #(go (>! channel %)))

;;     ac.decodeAudioData(request.response, callback)

;;     (defn load-audio [song-url]
;;       (let [c (chan)]
;;         (GET (str "/songs/" (url/url-encode song-url))
;;             {:handler #(go (>! c %))})
;;         c))
