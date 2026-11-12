(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneChargementDetailController', LigneChargementDetailController);

    LigneChargementDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LigneChargement'];

    function LigneChargementDetailController($scope, $rootScope, $stateParams, previousState, entity, LigneChargement) {
        var vm = this;

        vm.ligneChargement = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ligneChargementUpdate', function(event, result) {
            vm.ligneChargement = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
