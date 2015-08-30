(ns singularity.style
  (:require [garden.core :refer [css]]))

(def inline-block {:display "inline-block" :vertical-align "top"})

(def root (css 
  [:* { :box-sizing "border-box" }]
  [:body { 
    :user-select "none"
    :-moz-user-select "none"
    :-webkit-user-select "none"}]
  [:text {
    :font-size "20px"
    :cursor "default"}]
  [:#board (merge inline-block {
    :width "600px"
    :margin "10px"})]
  [:.panel (merge inline-block {
    :width "150px"})]
  [:input (merge inline-block { 
    :width "50px"
    :margin "none"})]
  [:label (merge inline-block { 
    :width "100px"})]))