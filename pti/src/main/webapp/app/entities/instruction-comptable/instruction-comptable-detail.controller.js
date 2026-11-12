(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InstructionComptableDetailController', InstructionComptableDetailController);

    InstructionComptableDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'InstructionComptable', 'TypeService'];

    function InstructionComptableDetailController($scope, $rootScope, $stateParams, previousState, entity, InstructionComptable, TypeService) {
        var vm = this;

        vm.instructionComptable = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:instructionComptableUpdate', function(event, result) {
            vm.instructionComptable = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
