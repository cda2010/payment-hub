(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DeviseDetailController', DeviseDetailController);

    DeviseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Devise', 'CompteBanque', 'Pays'];

    function DeviseDetailController($scope, $rootScope, $stateParams, previousState, entity, Devise, CompteBanque, Pays) {
        var vm = this;

        vm.devise = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:deviseUpdate', function(event, result) {
            vm.devise = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
