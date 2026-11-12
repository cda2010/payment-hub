(function() {
    "use strict";

    angular.module("ptiApp").factory('InitSteps', [function() {


        return {
            "initSteps": initSteps,
        };

        /**
         * Decodage des informations d'une carte grise
         */
        function initSteps(datas) {
            var vm=datas.vm;
            vm.selection = vm.steps[0];
            vm.error = false;
            vm.showOnglet=showOnglet;
            vm.getCurrentStepIndex = function() {
                return vm.steps.indexOf(vm.selection);
            };
             // Go to a defined step index
            vm.goToStepNotVerif = function(index, editForm) {
                if (!angular.isUndefined(vm.steps[index])) {
                    vm.selection = vm.steps[index];
                }
            };
    
            function showOnglet(onglet) {
                var stepIndex = vm.getCurrentStepIndex();
                return (vm.selection.toUpperCase() === onglet.toUpperCase());
            }
    
            // Go to a defined step index
            vm.goToStep = function(index, editForm) {
                if (!editForm.$invalid || (vm.getCurrentStepIndex() > index)) {
                    if (!angular.isUndefined(vm.steps[index])) {
                        console.log("change step");
                        vm.selection = vm.steps[index];
                    }
                }
            };
    
            vm.hasNextStep = function() {
                var stepIndex = vm.getCurrentStepIndex();
                var nextStep = stepIndex + 1;
                // Return true if there is a next step, false if not
                return !angular.isUndefined(vm.steps[nextStep]);
            };
    
            vm.hasPreviousStep = function() {
                var stepIndex = vm.getCurrentStepIndex();
                var previousStep = stepIndex - 1;
                // Return true if there is a next step, false if not
                return !angular.isUndefined(vm.steps[previousStep]);
            };
    
            vm.incrementStep = function() {
                if (vm.hasNextStep()) {
                    //Vérification des champs du step précedent
                    var stepIndex = vm.getCurrentStepIndex();
                    var nextStep = stepIndex + 1;
                    vm.selection = vm.steps[nextStep];
                }
            };
    
    
            vm.decrementStep = function() {
                if (vm.hasPreviousStep()) {
                    var stepIndex = vm.getCurrentStepIndex();
                    var previousStep = stepIndex - 1;
                    vm.selection = vm.steps[previousStep];
                }
            };
        }
    }]);
})();