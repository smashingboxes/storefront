(ns storefront.textify
  (:require [storefront.drawing]
            [storefront.helpers :refer :all]
            [quil.core :as q]
            [clojure.core.matrix :as m])
  (:import  [storefront.drawing Drawing]))

(def uppers (map char (range 66 92)))

(defn text-width [height text-str]
  (q/text-size height)
  (q/text-width text-str))

(defn make-text [image x y height text-str]
  (let [width          (text-width height text-str)
        clipped-width  (clamp width 0 (- (q/width) x))
        clipped-height (clamp height 0 (- (q/height) y))
        color          (average-color (q/get-pixel image x y clipped-width clipped-height))]
    (q/fill color)
    (q/text text-str x y)))

(defn loader
  ([path] (loader path false))
  ([path fill?]
    (let [image (q/load-image path)]
      (if fill? (q/background (average-color image)))
      (q/resize image (q/width) (q/height))
      image)))

(def cli-options
  [
    ["-i" "--image-path PATH" "Path to an image"
      :default "images/logo.png"]
    ["-l" "--letters-per-frame INT" "Number of letters to add each frame"
      :default 12
      :parse-fn #(Integer/parseInt %)]
    [nil "--min-letter-size INT" "Minumum letter height, in pixels"
      :default 12
      :parse-fn #(Integer/parseInt %)]
    [nil "--max-letter-size INT" "Maximum letter height, in pixels"
      :default 36
      :parse-fn #(Integer/parseInt %)]
  ])

(defn setup [options]
    (q/frame-rate 30)
    (let [image (loader (:image-path options) true)]
      { :image image
        :start (q/millis)
        :options options }))

(defn update-state [state] state)

(defn draw-state [state]
  (let [options      (:options state)
        at-a-time    (:letters-per-frame options)
        min-y        (:min-letter-size options)
        max-y        (:max-letter-size options)
        ys           (repeatedly at-a-time #(rand-in-range 0 (- (q/height) min-y)))
        xs           (repeatedly at-a-time #(rand-in-range 0 (q/width)))
        heights      (repeatedly at-a-time #(rand-in-range min-y max-y))
        text-strs    (repeatedly at-a-time #(str (rand-nth uppers)))
        zipped       (map vector xs ys heights text-strs)
        curried-text (fn [x y height text-str] (make-text (:image state) x y height text-str))]
    (doseq [zip zipped] (apply curried-text zip))))

(defn exit? [state] false)

(def drawing
  (Drawing. "Textify" setup update-state draw-state cli-options exit? nil))
