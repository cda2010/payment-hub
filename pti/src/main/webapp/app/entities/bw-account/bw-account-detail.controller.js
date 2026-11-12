(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BwAccountDetailController', BwAccountDetailController);

    BwAccountDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'BwAccount', 'BwCustomer'];

    function BwAccountDetailController($scope, $rootScope, $stateParams, previousState, entity, BwAccount, BwCustomer) {
        var vm = this;

        vm.bwAccount = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:bwAccountUpdate', function(event, result) {
            vm.bwAccount = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
