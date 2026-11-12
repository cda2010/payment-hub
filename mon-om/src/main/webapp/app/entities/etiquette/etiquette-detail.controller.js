(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('EtiquetteDetailController', EtiquetteDetailController);

    EtiquetteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Etiquette'];

    function EtiquetteDetailController($scope, $rootScope, $stateParams, previousState, entity, Etiquette) {
        var vm = this;

        vm.etiquette = entity;
        vm.previousState = previousState.name;
        console.log(vm.previousState);


        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:etiquetteUpdate', function(event, result) {
            vm.etiquette = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
