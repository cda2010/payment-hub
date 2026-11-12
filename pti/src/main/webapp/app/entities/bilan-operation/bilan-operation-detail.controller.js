(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BilanOperationDetailController', BilanOperationDetailController);

    BilanOperationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'BilanOperation', 'TypeService', 'Canal', 'Client', 'UniteOrganisation'];

    function BilanOperationDetailController($scope, $rootScope, $stateParams, previousState, entity, BilanOperation, TypeService, Canal, Client, UniteOrganisation) {
        var vm = this;

        vm.bilanOperation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:bilanOperationUpdate', function(event, result) {
            vm.bilanOperation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
