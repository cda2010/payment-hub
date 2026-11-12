(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('EtiquetteDetailController', EtiquetteDetailController);

    EtiquetteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Etiquette'];

    function EtiquetteDetailController($scope, $rootScope, $stateParams, previousState, entity, Etiquette) {
        var vm = this;

        vm.etiquette = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:etiquetteUpdate', function(event, result) {
            vm.etiquette = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
