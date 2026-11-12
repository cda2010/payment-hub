(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('RelationController', RelationController);

    RelationController.$inject = ['Relation'];

    function RelationController(Relation) {

        var vm = this;

        vm.relations = [];

        loadAll();

        function loadAll() {
            Relation.query(function(result) {
                vm.relations = result;
                vm.searchQuery = null;
            });
        }
    }
})();
