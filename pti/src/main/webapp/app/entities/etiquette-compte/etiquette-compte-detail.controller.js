(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteCompteDetailController', EtiquetteCompteDetailController);

    EtiquetteCompteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EtiquetteCompte'];

    function EtiquetteCompteDetailController($scope, $rootScope, $stateParams, previousState, entity, EtiquetteCompte) {
        var vm = this;

        vm.etiquetteCompte = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:etiquetteCompteUpdate', function(event, result) {
            vm.etiquetteCompte = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
