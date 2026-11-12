(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NationaliteDetailController', NationaliteDetailController);

    NationaliteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Nationalite', 'PersonnePhysique'];

    function NationaliteDetailController($scope, $rootScope, $stateParams, previousState, entity, Nationalite, PersonnePhysique) {
        var vm = this;

        vm.nationalite = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:nationaliteUpdate', function(event, result) {
            vm.nationalite = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
