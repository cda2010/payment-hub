(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EvenementOperationDetailController', EvenementOperationDetailController);

    EvenementOperationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'EvenementOperation'];

    function EvenementOperationDetailController($scope, $rootScope, $stateParams, previousState, entity, EvenementOperation) {
        var vm = this;

        vm.evenementOperation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:evenementOperationUpdate', function(event, result) {
            vm.evenementOperation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
