(ns singularity.core
  (:require [reagent.core :as r]
            [singularity.game :as game]
            [singularity.board :as board]
            [singularity.controls :as controls]
            [singularity.style :as style]
            [singularity.ai :as ai]))

(enable-console-print!)

(defn view-root
  "Top-level application component."
  []
  [:div
   [:style style/css]
   [board/board]
   [controls/control-panel]])

(defn init! []
  (game/init!)
  (board/init!)
  (ai/init!))

(r/render-component view-root (.getElementById js/document "app"))

(defn on-js-reload [] (init!))
(init!)