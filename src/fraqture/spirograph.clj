(ns fraqture.spirograph
  (:require [fraqture.drawing]
            [fraqture.helpers :refer :all]
            [quil.core :as q :include-macros true])
  (:import  [fraqture.drawing Drawing]))

(def current-color-path-location 0)
(def color-path
  [{:r 153 :g 255 :b 204}
   {:r 255 :g 0 :b 127}
   {:r 255 :g 153 :b 51}
   {:r 153 :g 204 :b 255}
   {:r 255 :g 255 :b 51}
   {:r 204 :g 153 :b 255}])

(defn setup [options]
  (q/frame-rate 30)
  (let [max-r (/ (q/width) 2)
        n (/ (q/width) 1.5)]
        ;; n (int (q/map-range (q/width) 100 130 20 30))]
   {:dots (into [] (for [r (map #(* max-r %) (range 0.05 1 (/ n)))]
                        [r 0]))
    :bg-color (nth color-path 0)}))

(defn speed[]
  (+ 0.00016 (* 0.0001 (q/sin (* (q/millis) 0.00017)))))

(defn move [dot]
  (let [[r a] dot]
    [r (+ a (* r (speed)))]))

(defn next-color-index []
  (if (= (+ 1 current-color-path-location) (count color-path))
    (def current-color-path-location 0))
    (def current-color-path-location (inc current-color-path-location)))

(defn walk-color-channel [current upcoming]
  (if (= upcoming current)
    current
    (if (> upcoming current)
      (inc current)
      (dec current))))

(defn walk-color[current-bg-color]
  (apply q/background (vals current-bg-color))
  (let [upcoming-color (nth color-path current-color-path-location)]
    (if (= current-bg-color upcoming-color)
      (do
        (next-color-index)
        (walk-color current-bg-color))
      {:r (walk-color-channel (:r current-bg-color) (:r upcoming-color))
       :g (walk-color-channel (:g current-bg-color) (:g upcoming-color))
       :b (walk-color-channel (:b current-bg-color) (:b upcoming-color))})))

(defn update-state [state]
  (-> state
    (update-in [:dots] #(map move %))
    (update-in [:bg-color] #(walk-color %))))

(defn dot->coord [[r a]]
  [(+ (/ (q/width) 2) (* r (q/cos a)))
   (+ (/ (q/height) 2) (* r (q/sin a)))])

(defn draw-state [state]
  (q/fill 20 230 150)
  (q/no-stroke)
  (let [dots (:dots state)]
    (loop [curr (first dots)
           tail (rest dots)
           prev nil]
      (let [[x y] (dot->coord curr)]
        (let [size 25]
          (q/ellipse x y size size)))
      (when (seq tail)
        (recur (first tail)
               (rest tail)
               curr)))))

(def drawing
 (Drawing. "Spirograph" setup update-state draw-state nil nil nil))
