(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationDetailController', OperationDetailController);

    OperationDetailController.$inject = ['$scope', '$rootScope', 'previousState', 'entity', 'Operation', 'TypeService', 'Beneficiaire', 'Emetteur', 'PointVente', 'Pays', 'Canal'];

    function OperationDetailController($scope, $rootScope, previousState, entity, Operation, TypeService, Beneficiaire, Emetteur, PointVente, Pays, Canal) {
        var vm = this;

        vm.operation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:operationUpdate', function(event, result) {
            vm.operation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
