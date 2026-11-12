(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteComptableDetailController', EtiquetteComptableDetailController);

    EtiquetteComptableDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EtiquetteComptable'];

    function EtiquetteComptableDetailController($scope, $rootScope, $stateParams, previousState, entity, EtiquetteComptable) {
        var vm = this;

        vm.etiquetteComptable = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:etiquetteComptableUpdate', function(event, result) {
            vm.etiquetteComptable = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
