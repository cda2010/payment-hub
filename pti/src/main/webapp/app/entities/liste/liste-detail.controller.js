(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeDetailController', ListeDetailController);

    ListeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Liste'];

    function ListeDetailController($scope, $rootScope, $stateParams, previousState, entity, Liste) {
        var vm = this;

        vm.liste = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:listeUpdate', function(event, result) {
            vm.liste = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
