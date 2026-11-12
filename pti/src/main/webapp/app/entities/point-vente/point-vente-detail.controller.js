(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PointVenteDetailController', PointVenteDetailController);

    PointVenteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'PointVente', 'Operation', 'UniteOrganisation'];

    function PointVenteDetailController($scope, $rootScope, $stateParams, previousState, entity, PointVente, Operation, UniteOrganisation) {
        var vm = this;

        vm.pointVente = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:pointVenteUpdate', function(event, result) {
            vm.pointVente = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
