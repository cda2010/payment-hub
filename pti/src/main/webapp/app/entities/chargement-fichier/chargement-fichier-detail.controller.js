(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementFichierDetailController', ChargementFichierDetailController);

    ChargementFichierDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ChargementFichier'];

    function ChargementFichierDetailController($scope, $rootScope, $stateParams, previousState, entity, ChargementFichier) {
        var vm = this;

        vm.ChargementFichier = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:chargementGFichierUpdate', function(event, result) {
            vm.ChargementFichier = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
