(ns vashet.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [vashet.vashet-test]))

(doo-tests 'vashet.vashet-test)
