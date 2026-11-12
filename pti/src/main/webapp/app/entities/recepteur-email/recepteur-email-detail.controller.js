(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurEmailDetailController', RecepteurEmailDetailController);

    RecepteurEmailDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'RecepteurEmail'];

    function RecepteurEmailDetailController($scope, $rootScope, $stateParams, previousState, entity, RecepteurEmail) {
        var vm = this;

        vm.recepteurEmail = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:recepteurEmailUpdate', function(event, result) {
            vm.recepteurEmail = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
