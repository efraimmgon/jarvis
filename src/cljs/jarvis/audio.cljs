(ns jarvis.audio
  (:require
    [clojure.string :as string]
    [jarvis.utils.events :refer [base-interceptors js->edn query]]
    [re-frame.core :as rf]
    [reagent.core :as r]))


;;; ---------------------------------------------------------------------------
;;; Core
;;; ---------------------------------------------------------------------------

(defonce audio-recorder
  (r/atom
    {:audio-blobs []
     :media-recorder nil
     :stream-being-captured nil}))

(defonce app-db
  (r/atom 
    {:audio-record/status :idle
     :audio-record/start-time nil
     :audio/status nil}))

(defn start 
  "Start recording the audio. Returns a promise that resolves if audio 
   recording successfully started"
  []
  (if-not (and js/navigator.mediaDevices js/navigator.mediaDevices.getUserMedia)
    (js/Promise.reject (js/Error. "mediaDevices API or getUserMedia method is not supported in this browser."))
    
    (do
      (js/console.log "Feature is supported in browser")
      
      (-> 
        (js/navigator.mediaDevices.getUserMedia
          #js {:audio true})
        (.then 
          (fn [stream]
            ;; save the reference of the stream to be able to stop it when 
            ;; necessary
            (swap! audio-recorder assoc :stream-being-captured stream)
            
            ;; create a media recorder instance by passing that stream into the 
            ;; MediaRecorder constructor
            (swap! audio-recorder assoc :media-recorder (js/MediaRecorder. stream))

            ;; clear previously saved audio Blobs, if any
            (swap! audio-recorder assoc :audio-blobs [])
            
            ;; add a dataavailable event listener in order to store the audio 
            ;; data Blobs when recording
            (.addEventListener (:media-recorder @audio-recorder) 
              "dataavailable"
              (fn [event]
                (swap! audio-recorder update :audio-blobs conj (.-data event))))
            
            ;; start the recording
            (.start (:media-recorder @audio-recorder))))
        (.catch
          (fn [error]
            (swap! app-db assoc :audio-record/status :not-allowed)
            (js/console.log (.-name error) (.-message error))))))))


(defn stop-stream 
  "Stop all the tracks on the active stream in order to stop the stream and 
   remove the red flashing dot showing in the tab."
  []
  (let [stream-being-captured (:stream-being-captured @audio-recorder)]
    (doseq [track (.getTracks stream-being-captured)]
      (.stop track))))


(defn reset-recording-properties 
  "Reset all the recording values, including the media recorder and stream 
   being captured."
  []
  (swap! audio-recorder assoc :media-recorder nil)
  (swap! audio-recorder assoc :stream-being-captured nil))


(defn cancel 
  "Cancel audio recording."
  []
  (.stop (:media-recorder @audio-recorder))
  (stop-stream)
  (reset-recording-properties))


(defn stop 
  "Stop the started audio recording. Returns a promise that resolves to the 
   audio as a blob file."
  []
  (js/Promise.
    (fn [resolve _reject]
      (let [mime-type (.-mimeType (:media-recorder @audio-recorder))]
        (.addEventListener (:media-recorder @audio-recorder) 
          "stop"
          (fn []
            ;; create a single blob object, as we might have gathered a few 
            ;; Blob objects that needs to be joined as one
            (let [audio-blob (js/Blob. (:audio-blobs @audio-recorder) 
                               #js {:type mime-type})]
              (resolve audio-blob))))
        (cancel)))))


;;; ---------------------------------------------------------------------------
;;; Hanlders
;;; ---------------------------------------------------------------------------

; send the user request to whisper api
; take the response and send it to gpt


;;; ---------------------------------------------------------------------------
;;; Helpers
;;; ---------------------------------------------------------------------------


(declare handle-elapsed-recording-time)


(defn start-audio-recording
  []
  
  (println "Recording audio...")
  
  (handle-elapsed-recording-time)
  
  (->
    (start)
    (.then
      (fn []
        ;; store the recording start time to display the elapsed time according to it
        (swap! app-db assoc :audio-record/start-time (js/Date.))
        (swap! app-db assoc :audio-record/status :recording)))
    (.catch
      (fn [error]
        (when 
          (-> error .-message 
            (.includes 
              (str "mediaDevices API or getUserMedia method is not supported"
                " in this browser.")))
          (js/alert "To record audio, use browsers like Chrome and Firefox."))))))


(defn cancel-audio-recording!
  []
  (println "Canceling audio...")
  (swap! app-db assoc :audio-record/status :idle)
  (js/clearInterval (:audio-recorder/timer-obj @app-db))
  (cancel))


(defn play-audio!
  "Plays recorded audio using the audio element in the HTML document"
  [audio-src]
  (let [audio-obj ^js (js/Audio. audio-src)]
    
    (swap! app-db assoc 
      :audio/obj audio-obj
      :audio/status :playing)
    
    (set! (.-onended audio-obj)
      (fn []
        (swap! app-db assoc :audio/status :idle)))
    
    (.play audio-obj)))


(defn stop-audio-recording! 
  "Stop the currently started audio recording & plays it"
  []
  (println "Stopping audio recording...")
  (-> (stop)
    (.then 
      (fn [blob]
        (swap! app-db assoc
          :audio-record/status :idle
          :audio/src (js/window.URL.createObjectURL blob))
        (js/clearInterval (:audio-recorder/timer-obj @app-db))))))


(defn maybe-pad-with-0 [n]
  (if (< n 10)
    (str "0" n)
    n))


(defn compute-elapsed-time [start-time]
  (let [end-time (js/Date.)
        time-diff (-> end-time (- start-time) (/ 1000) int) 
        seconds (-> time-diff (mod 60) int)
        time-diff-minutes (-> time-diff (/ 60) int)
        minutes (mod time-diff-minutes 60)
        time-diff-hours (-> time-diff-minutes (/ 60) int)
        hours (mod time-diff-hours 24)
        days (-> time-diff-hours (/ 24) int)
        total-hours (-> days (* 24) (+ hours))]
    (if (zero? total-hours)
      (str (maybe-pad-with-0 minutes) ":" (maybe-pad-with-0 seconds))
      (str 
        (maybe-pad-with-0 total-hours) ":" (maybe-pad-with-0 minutes) ":"
        (maybe-pad-with-0 seconds)))))


(defn handle-elapsed-recording-time 
  "Computes the elapsed recording time since the moment the function is called 
   in the format h:m:s"
  []
  (swap! app-db assoc :audio-recorder/timer "00:00")

  ;; create an interval that computes & displays elapsed time, as well as 
  ;; animates red dot - every second
  (let [elapsed-time-timer 
        (js/setInterval
          (fn []
            (let [elapsed-time (compute-elapsed-time (:audio-record/start-time @app-db))]
              (swap! app-db assoc :audio-recorder/timer elapsed-time)))
          1000)]
    (swap! app-db assoc :audio-recorder/timer-obj elapsed-time-timer)))


(comment
  (compute-elapsed-time
    (-> (js/Date.)
      (.getTime)
      (- (* 2 24 43 24 1000))
      (js/Date.))))

(defn pause-audio []
  (let [audio (:audio/obj @app-db)]
    (.pause audio)))


(defn audio-recorder-recording? []
  (= :recording (:audio-record/status @app-db)))


(defn audio-record-idle? []
  (= :idle (:audio-record/status @app-db)))


(defn playing? []
  (= :playing (:audio/status @app-db)))


;;; ---------------------------------------------------------------------------
;;; Text to Speech
;;; ---------------------------------------------------------------------------

(defn get-voice [voices uri]
  (.find voices
    (fn [v] (= (.-voiceURI v) uri) )))


(defn text->speech [text]
  (if-not (aget js/window "speechSynthesis")
    (js/alert "Sorry, your browser doesn't support text to speech!")
    
    (let [msg (js/SpeechSynthesisUtterance.)
          ; Good options: Daniel, Karen (Enhanced)
          voice (.find (js/speechSynthesis.getVoices) 
                  #(= (.-voiceURI %) "Karen (Enhanced)"))] 
      (set! (.-voice msg) voice)
      (set! (.-text msg) text)
      (js/window.speechSynthesis.speak msg))))

(comment
  (text->speech "OK, maybe I’m biased because I love text to speech. ")
  
  (def voices (js/speechSynthesis.getVoices))

  (filter (fn [^js v] 
            (.includes (.-lang v) "en"))
    voices))


;;; ---------------------------------------------------------------------------
;;; Speech to Text
;;; ---------------------------------------------------------------------------

(defn set-speech-recognition-window-obj! []
  (when-not js/window.SpeechRecognition
    (let [speech-recognition 
          (or js/window.SpeechRecognition js/window.webkitSpeechRecognition)]
      
      (set! (.-SpeechRecognition js/window) speech-recognition))))
  

(defn set-speech-to-text! [^js recognition]
  (set! (.-interimResults recognition) true)
  (set! (.-continuous recognition) true)
  (set! (.-lang recognition) "en-US")
  
  (set! (.-onend recognition)
    (fn [_e]
      (println "Speech recognition ended")))
  
  (set! (.-onstart recognition)
    (fn [_e]
      (println "Speech recognition started")))
  
  (set! (.-onresult recognition)
    (fn [e]
      (println "Confidence:" (some-> e .-results ffirst .-confidence))
      (let [transcript 
            (->> (js/Array.from (.-results e))
              (map first)
              (map #(.-transcript %))
              (string/join ""))]
        (rf/dispatch [:assoc-in [:audio/transcription] transcript]))))
  
  (set! (.-onerror recognition)
    (fn [err]
      (println "Error occurred in recognition:" (.-error err) (.-message err))))
      
  (.start recognition))


(defn stop-speech-recognition! []
  (swap! app-db assoc :audio-record/status :idle)
  (.stop ^js @(rf/subscribe [:audio/speech-recognition-obj])))


(rf/reg-event-db
  :audio/text-to-speech
  base-interceptors
  (fn [db _]
    (set-speech-recognition-window-obj!)
    (swap! app-db assoc :audio-record/status :recording)
    (let [recognition (js/SpeechRecognition.)]
      (set-speech-to-text! recognition)
      (println "speech to text set!")
      (assoc db 
        :audio/speech-recognition-obj recognition))))


(rf/reg-sub :audio/transcription query)
(rf/reg-sub :audio/speech-recognition-obj query)



;;; ---------------------------------------------------------------------------
;;; UI
;;; ---------------------------------------------------------------------------


(defn audio-ui []
  [:div
   ;[c/pretty-display @audio-recorder]
   ;[c/pretty-display @app-db]
   [:div
    
    (when (audio-record-idle?)
      [:button.btn.btn-primary
       ;{:on-click start-audio-recording}
       {:on-click #(rf/dispatch [:audio/text-to-speech])}
       "Send audio message "
       [:i.fa.fa-microphone]]) " "

    (when (audio-recorder-recording?)
      [:div
       (let [elapsed-time (:audio-recorder/timer @app-db)]
         [:p [:i.fa.fa-microphone] " Recording... " elapsed-time])
       
       " "
       
       [:button.btn.btn-danger
        {:on-click cancel-audio-recording!}
        "Cancel recording "
        [:i.fa.fa-times-circle-o]]
       " "
       
       [:button.btn.btn-primary
        ;{:on-click stop-audio-recording!}
        {:on-click stop-speech-recognition!}
        "End recording "
        [:i.fa.fa-stop-circle-o]]])
    
    #_ ;; audio controls
    [:div
     (when-let [src (:audio/src @app-db)]
         
       [:div
          
        [:audio
         {:src src
          :controls true}]])]]
   
   (when (= :not-supported (:audio-record/status @app-db))
     [:p
      "To record audio, use browsers like Chrome and Firefox that support audio recording."])
   
   (when (= :not-allowed (:audio-record/status @app-db))
     [:p
      "To record audio, use browsers like Chrome and Firefox that support audio recording."])])