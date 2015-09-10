(ns singularity.style
	(:require [garden.core :as garden]
            [garden.selectors :as sel]))

(def sq-text "#9999aa")
(def sq-white "#f0f0f0")
(def sq-black "#c6d6f6")
(def sq-sel "yellow")
(def sq-valid "#aaeeaa")
(def sq-invalid "#eeaaaa")
(def sq-border "#9999aa")
(def text "#777788")
(def text-input "#8888aa")

(def ^:private inline-block {:display "inline-block" :vertical-align "top"})

(def css (garden/css 
  [:* { :box-sizing "border-box" }]
  [:body { 
    :user-select "none"
    :-moz-user-select "none"
    :-webkit-user-select "none"
    :font "14px Calibri,Arial,sans-serif"}]
  [(keyword "g:nth-child(2n) path") {:fill sq-black}
    [:&.sel {:fill sq-sel}]
    [:&.valid:hover {:fill sq-valid}]
    [:&.invalid:hover {:fill sq-invalid}]]
  [:path {
    :fill sq-white
    :stroke sq-text}
    [:&.sel {:fill sq-sel}]
    [:&.valid:hover {:fill sq-valid}]
    [:&.invalid:hover {:fill sq-invalid}]]
  [:text {
    :font-size "20px"
    :cursor "default"
    :fill sq-text}]
  [:#board (into inline-block {
    :width "600px"
    :margin "10px"})]
  [:.panel (into inline-block {
    :width "150px"})]
  [:input (into inline-block { 
    :width "30px"
    :margin "0px 5px"
    :border "none"
    :color text-input
    :background-color sq-white
    :border-radius "4px"})]
  [:p (into inline-block {:color text})]
  [:a.input {
    :text-decoration "underline"
    :color text-input
    :cursor "pointer"}]
  [:.valid {
    :background-color "red"}]))