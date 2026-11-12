(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TransactionComptePtiDetailController', TransactionComptePtiDetailController);

    TransactionComptePtiDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TransactionComptePti', 'Operation'];

    function TransactionComptePtiDetailController($scope, $rootScope, $stateParams, previousState, entity, TransactionComptePti, Operation) {
        var vm = this;

        vm.transactionComptePti = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:transactionComptePtiUpdate', function(event, result) {
            vm.transactionComptePti = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
