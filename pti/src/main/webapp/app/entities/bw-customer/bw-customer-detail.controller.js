(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BwCustomerDetailController', BwCustomerDetailController);

    BwCustomerDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'BwCustomer'];

    function BwCustomerDetailController($scope, $rootScope, $stateParams, previousState, entity, BwCustomer) {
        var vm = this;

        vm.bwCustomer = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:bwCustomerUpdate', function(event, result) {
            vm.bwCustomer = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
