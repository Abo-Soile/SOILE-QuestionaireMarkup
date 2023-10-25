mvn install -DcreateChecksum=true
cp -r ~/.m2/repository/fi/abo/kogni/soile2/qmarkup/* ~/Work/gitRepos/SOILE-MavenArtifacts/fi/abo/kogni/soile2/qmarkup/
cd ~/Work/gitRepos/SOILE-MavenArtifacts/fi/abo/kogni/soile2/
git add *
git commit -m "$1"
git push
cd -
