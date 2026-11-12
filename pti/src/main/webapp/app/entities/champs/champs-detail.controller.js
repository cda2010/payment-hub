(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChampsDetailController', ChampsDetailController);

    ChampsDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Champs', 'Composant'];

    function ChampsDetailController($scope, $rootScope, $stateParams, previousState, entity, Champs, Composant) {
        var vm = this;

        vm.champs = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:champsUpdate', function(event, result) {
            vm.champs = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
