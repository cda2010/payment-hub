(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonneMoraleDetailController', PersonneMoraleDetailController);

    PersonneMoraleDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'PersonneMorale'];

    function PersonneMoraleDetailController($scope, $rootScope, $stateParams, previousState, entity, PersonneMorale) {
        var vm = this;

        vm.personneMorale = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:personneMoraleUpdate', function(event, result) {
            vm.personneMorale = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
