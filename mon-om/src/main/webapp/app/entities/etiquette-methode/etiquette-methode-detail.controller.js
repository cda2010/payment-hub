(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('EtiquetteMethodeDetailController', EtiquetteMethodeDetailController);

    EtiquetteMethodeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EtiquetteMethode'];

    function EtiquetteMethodeDetailController($scope, $rootScope, $stateParams, previousState, entity, EtiquetteMethode) {
        var vm = this;

        vm.etiquetteMethode = entity;
        vm.previousState = previousState.name;
        console.log(vm.previousState);
        var unsubscribe = $rootScope.$on('omnicanalPresentationApp:etiquetteUpdate', function(event, result) {
            vm.etiquetteMethode = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
