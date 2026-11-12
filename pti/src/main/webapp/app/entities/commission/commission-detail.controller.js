(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CommissionDetailController', CommissionDetailController);

    CommissionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Commission', 'Operation', 'UniteOrganisation'];

    function CommissionDetailController($scope, $rootScope, $stateParams, previousState, entity, Commission, Operation, UniteOrganisation) {
        var vm = this;

        vm.commission = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:commissionUpdate', function(event, result) {
            vm.commission = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
