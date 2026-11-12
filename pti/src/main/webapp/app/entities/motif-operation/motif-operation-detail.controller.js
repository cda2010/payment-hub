(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MotifOperationDetailController', MotifOperationDetailController);

    MotifOperationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'MotifOperation'];

    function MotifOperationDetailController($scope, $rootScope, $stateParams, previousState, entity, MotifOperation) {
        var vm = this;

        vm.motifOperation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:motifOperationUpdate', function(event, result) {
            vm.motifOperation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
